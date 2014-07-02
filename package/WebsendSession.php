<?php
class WebsendSession {
	/**
	 * @var Websend context.
	 */
	private $ws;
	private $sessionID;
	private $sessionClosed = false;

	function __construct($websend, $id){
		$this->ws = $websend;
		$this->sessionID = $id;
	}

	function __destruct(){
		if(!$this->sessionClosed){
			$this->closeSession();
		}
	}

	/**
	 * Retrieves a java method based on the type of the objectID parameter,
	 * the method name and the method arguments.
	 *
	 * For example, to get "boolean startsWith(String prefix, int toffset)" from a string object you use:
	 * getMethodByObject($stringObjID, "startsWith", "Ljava.lang.String;I");
	 *
	 * @param int $objectID Parent object ID.
	 * @param string $methodName The name of the method.
	 * @param string $argTypes The type of the arguments provided in the format specified in JVM spec 4.3. (no returntype)
	 * @return int The ID of the requested method. See invoke for usage.
	 */
	function getMethodByObject($objectID, $methodName, $argTypes){
		if($this->sessionClosed){
			throw new Exception("A closed session cannot be used!");
		}
		$this->ws->javaStream->writeByte(32);
		$this->ws->javaStream->writeInt($this->sessionID);
		$this->ws->javaStream->writeInt($objectID);
		$this->ws->javaStream->writeString($methodName);
		$this->ws->javaStream->writeString($argTypes);
		return $this->ws->javaStream->readInt();
	}

	/**
	 * Retrieves a java method based on the classname the method name and the method arguments.
	 *
	 * For example, to get "static String format(String format, Object... args) " from String you use:
	 * getMethodByObject("java.lang.String", "format", "Ljava.lang.String;[Ljava.lang.Object;");
	 *
	 * @param string $className Name of class that contains the method.
	 * @param string $methodName The name of the method.
	 * @param string $argTypes The type of the arguments provided in the format specified in JVM spec 4.3. (no returntype)
	 * @return int The ID of the requested method. See invoke for usage.
	 */
	function getMethodByClass($className, $methodName, $argTypes){
		if($this->sessionClosed){
			throw new Exception("A closed session cannot be used!");
		}
		$this->ws->javaStream->writeByte(33);
		$this->ws->javaStream->writeInt($this->sessionID);
		$this->ws->javaStream->writeString($className);
		$this->ws->javaStream->writeString($methodName);
		$this->ws->javaStream->writeString($argTypes);
		return $this->ws->javaStream->readInt();
	}

	/**
	 * Run a java method on a previously retrieved object with the specified arguments.
	 * For static methods see invokeStatic.
	 *
	 * @param int $objectID The object to call the method on.
	 * @param int $methodID The method to call
	 * @param string $argTypes The type of the arguments provided in the format specified in JVM spec 4.3. (no returntype)
	 * @param mixed $args,... The arguments.
	 * @return mixed Returns the object returned by the java method.
	 */
	function invoke($objectID, $methodID, $argTypes){
		if($this->sessionClosed){
			throw new Exception("A closed session cannot be used!");
		}
		$args = array_slice(func_get_args(), 3);
		$this->ws->javaStream->writeByte(34);
		$this->ws->javaStream->writeInt($this->sessionID);
		$this->ws->javaStream->writeInt($objectID);
		$this->ws->javaStream->writeInt($methodID);
		$this->serializeObjectsAsTypeString($argTypes, $args);
		$returnType = $this->ws->javaStream->readString();
		return $this->deserializeObjectsFromTypeString($returnType);
	}

	/**
	 * Run a static java method on a class.
	 *
	 * @param int $methodID The method to call
	 * @param string $argTypes The type of the arguments provided in the format specified in JVM spec 4.3. (no returntype)
	 * @param mixed $args,... The arguments.
	 * @return mixed Returns the object returned by the java method.
	 */
	function invokeStatic($methodID, $argTypes){
		if($this->sessionClosed){
			throw new Exception("A closed session cannot be used!");
		}
		$args = array_slice(func_get_args(), 2);
		$this->ws->javaStream->writeByte(35);
		$this->ws->javaStream->writeInt($this->sessionID);
		$this->ws->javaStream->writeInt($methodID);
		$this->serializeObjectsAsTypeString($argTypes, $args);
		$returnType = $this->ws->javaStream->readString();
		return $this->deserializeObjectsFromTypeString($returnType);
	}

	/**
	 * Releases an object from the servers object cache.
	 * This will make the object ID invalid and inoperative for further usage.
	 * The object will automatically be released when the session ends or
	 * you can use this function to do this manually.
	 */
	function releaseObject($objectID){
		if($this->sessionClosed){
			throw new Exception("A closed session cannot be used!");
		}
		$this->ws->javaStream->writeByte(36);
		$this->ws->javaStream->writeInt($this->sessionID);
		$this->ws->javaStream->writeInt($objectID);
	}

	/**
	 * Releases a method from the servers method cache.
	 * This will make the method ID invalid and inoperative for further usage.
	 * The java method will automatically be released when the session ends or
	 * you can use this function to do this manually.
	 */
	function releaseMethod($methodID){
		if($this->sessionClosed){
			throw new Exception("A closed session cannot be used!");
		}
		$this->ws->javaStream->writeByte(37);
		$this->ws->javaStream->writeInt($this->sessionID);
		$this->ws->javaStream->writeInt($methodID);
	}

	/**
	 * Ends the current session. All objects and methods are released and their IDs become invalid.
	 * After endSession is called, no methods or objects retrieved using this session can be used.
	 * This method is automatically called in the object deconstructor.
	 */
	function closeSession(){
		$this->sessionClosed = true;
		$this->ws->javaStream->writeByte(31);
		$this->ws->javaStream->writeInt($this->sessionID);
	}

	private function serializeObjectsAsTypeString($typeStr, $objects){
		$this->ws->javaStream->writeString($typeStr);
		$chars = str_split($typeStr);
		$readingClassName = false;
		$curClassName = "";

		$objectI = 0;
		for($i = 0;$i<count($chars);$i++){
			$char = $chars[$i];
			$lowerChar = strtolower($char);
			$obj = $objects[$objectI];
			if($readingClassName){
				if($char === ';'){
					$readingClassName = false;
					//Handle $curClassName!
					if($curClassName === "java.lang.String" && is_string($obj)){
						$this->ws->javaStream->writeString($obj);
					}else{
						$this->ws->javaStream->writeInt($obj);
					}
				}else{
					$curClassName = $curClassName . $char;
					continue;
				}
			}else if($lowerChar === 'n'){
				//null
			}else if($lowerChar === 'z'){
				$this->ws->javaStream->writeBoolean($obj);
			}else if($lowerChar === 'b'){
				$this->ws->javaStream->writeByte($obj);
			}else if($lowerChar === 'c'){
				$this->ws->javaStream->writeChar($obj);
			}else if($lowerChar === 's'){
				$this->ws->javaStream->writeShort($obj);
			}else if($lowerChar === 'i'){
				$this->ws->javaStream->writeInt($obj);
			}else if($lowerChar === 'j'){
				$this->ws->javaStream->writeLong($obj);
			}else if($lowerChar === 'f'){
				$this->ws->javaStream->writeFloat($obj);
			}else if($lowerChar === 'd'){
				$this->ws->javaStream->writeDouble($obj);
			}else if($lowerChar === 'l'){
				$readingClassName = true;
				continue;
			}
			$objectI++;
		}
	}

	private function deserializeObjectsFromTypeString($typeStr){
		$chars = str_split(strtolower($typeStr));
		$readingClassName = false;
		$curClassName = "";

		$objects = array();
		$objectI = 0;
		for($i = 0;$i<count($chars);$i++){
			$char = $chars[$i];
			$lowerChar = strtolower($char);
			if($readingClassName){
				if($char === ';'){
					$readingClassName = false;
					if($curClassName === "java.lang.String"){
						$objects[$objectI] = $this->ws->javaStream->readString();
					}else{
						$objects[$objectI] = $this->ws->javaStream->readInt();
					}
				}else{
					$curClassName = $curClassName . $char;
					continue;
				}
			}else if($lowerChar === 'n'){
				$objects[$objectI] = NULL;
			}else if($lowerChar === 'z'){
				$objects[$objectI] = $this->ws->javaStream->readBoolean();
			}else if($lowerChar === 'b'){
				$objects[$objectI] = $this->ws->javaStream->readByte();
			}else if($lowerChar === 'c'){
				$objects[$objectI] = $this->ws->javaStream->readChar();
			}else if($lowerChar === 's'){
				$objects[$objectI] = $this->ws->javaStream->readShort();
			}else if($lowerChar === 'i'){
				$objects[$objectI] = $this->ws->javaStream->readInt();
			}else if($lowerChar === 'j'){
				$objects[$objectI] = $this->ws->javaStream->readLong();
			}else if($lowerChar === 'd' || $char == 'f'){
				$objects[$objectI] = $this->ws->javaStream->readDouble();
			}else if($lowerChar === 'l'){
				$readingClassName = true;
				continue;
			}
			$objectI++;
		}
		if(count($objects) == 1){
			return $objects[0];
		}else{
			return $objects;
		}
	}

	function getID(){
		return $this->sessionID;
	}
}
?>