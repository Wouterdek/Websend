<?php
include_once "WebsendSession.php";

class Websend
{
	public $timeout = 3600;/* Connection timeout as defined in fsockopen */
    public $password = "";/* Password in Websend server config */
    public $hashAlgorithm = "sha512";

	/**
	 * @var JavaStream Stream with Java datatypes. For internal use only.
	 */
	public $javaStream;

    var $host;
    var $port;
	var $stream;

    public function __construct($host, $port = 4445)
    {
        $this->host = $host;
        $this->port = $port;
    }

    public function __destruct(){
        if($this->stream){
            $this->disconnect();
        }
    }

    /**
     * Connects to a Websend server.
     * Returns true if successful.
     */
    public function connect()
    {
        $this->stream = fsockopen($this->host, $this->port,$errno,$errstr,$this->timeout);
        if($this->stream){
			$this->javaStream = new JavaStream($this->stream);
            $this->javaStream->writeByte(21);
            $this->javaStream->writeString("websendmagic");
            $seed = $this->javaStream->readInt();
            $hashedPassword = hash($this->hashAlgorithm, $seed.$this->password);
            $this->javaStream->writeString($hashedPassword);
            $result = $this->javaStream->readInt();

            if($result == 1){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * Sends a disconnect signal to the currently connected Websend server.
     */
    public function disconnect()
    {
        $this->javaStream->writeByte(20);
    }

	/**
	 * Opens a session. This allows you to access Bukkit's internal java structure through PHP.
	 * @return WebsendSession A new, open session to use.
	 */
	public function openSession(){
		$this->javaStream->writeByte(30);
		$sessionID = $this->javaStream->readInt();
		return new WebsendSession($this, $sessionID);
	}

	/**
	 * Ends a session. All objects and methods are released and their IDs become invalid.
	 * After a session is closed, no methods or objects retrieved using that session can be used.
	 */
	public function closeSession(WebsendSession $session){
		$session->closeSession();
	}

    /**
     * Run a command as if the specified player typed it into the chat.
     *
     * @param string $cmmd Command and arguments to run.
     * @param string $playerName Exact name of the player to run it as.
     * @return true if the command and player were found, else false
     */
    public function doCommandAsPlayer($cmmd, $playerName)
    {
        $this->javaStream->writeByte(1);
        $this->javaStream->writeString($cmmd);
        if(isset($playerName))
        {
            $this->javaStream->writeString($playerName);
        }
        else
        {
            $this->javaStream->writeString("null");
        }

        if($this->javaStream->readInt() == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Run a command as if it were typed into the console.
     *
     * @param string $cmmd Command and arguments to run.
     * @return true if the command was found, else false
     */
    public function doCommandAsConsole($cmmd)
    {
        $this->javaStream->writeByte(2);
        $this->javaStream->writeString($cmmd);

        if($this->javaStream->readInt() == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Run a script.
     * The script has to be in the Websend scripts directory and has to be compiled and loaded before this is runned.
     *
     * @param string $scriptName Name of the script.
     */
    public function doScript($scriptName)
    {
        $this->javaStream->writeByte(3);
        $this->javaStream->writeString($scriptName);
    }

    /**
     * Start plugin output capture
     *
     * @param string $pluginName Name of the plugin.
     */
    public function startPluginOutputListening($pluginName)
    {
        $this->javaStream->writeByte(4);
        $this->javaStream->writeString($pluginName);
    }

    /**
     * Stop plugin output capture
     *
     * @param string $pluginName Name of the plugin.
     * @return array of strings that contains output.
     */
    public function stopPluginOutputListening($pluginName)
    {
        $this->javaStream->writeByte(5);
        $this->javaStream->writeString($pluginName);
        $size = $this->javaStream->readInt();
        $data = array();
        for($i = 0; $i<$size;$i++){
            $messageSize = $this->javaStream->readInt();
            $data[$i] = $this->javaStream->readChars($messageSize);
        }
        return $data;
    }

    /**
     * Print output to the console window. Invisible to players.
     */
    public function writeOutputToConsole($message)
    {
        $this->javaStream->writeByte(10);
        $this->javaStream->writeString($message);
    }

    /**
     * Prints output to specified player.
     *
     * @param string $message Message to be shown.
     * @param string $playerName Exact name of the player to print the message to.
     * @return true if the player was found, else false
     */
    public function writeOutputToPlayer($message, $playerName)
    {
        $this->javaStream->writeByte(11);
        $this->javaStream->writeString($message);
        if(isset($playerName))
        {
            $this->javaStream->writeString($playerName);
        }
        else
        {
            $this->javaStream->writeString("null");
        }

        if($this->javaStream->readInt() == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Prints a message to all players and the console.
     *
     * @param string $message Message to be shown.
     */
    public function broadcast($message)
    {
        $this->javaStream->writeByte(12);
        $this->javaStream->writeString($message);
    }
}

class JavaStream{
	private $stream;

	function __construct($stream){
		$this->stream = $stream;
	}

	/*********************/
	/****** WRITING ******/
	/*********************/

	public function writeMixed( $object )
	{
		if(is_null($object)){
			throw new Exception("Cannot write null object!");
		}elseif(is_bool($object)){
			$this->writeBoolean($object);
		}elseif(is_float($object)){
			$this->writeDouble($object);
		}elseif(is_int($object)){
			//TODO: This seems like a bad idea. This will generate bugs because
			//TODO: you can't predict whether 4 or 8 bytes will be sent, except if you know the value.
			if(PHP_INT_SIZE == 8 && ($object > 0x7FFFFFFF || $object < -0x80000000)){
				$this->writeLong($object);
			}else{
				$this->writeInt($object);
			}
		}elseif(is_string($object)){
			$this->writeString($object);
		}else{
			throw new Exception("Object cannot be serialized to simple data type!");
		}
	}

	public function writeBoolean( $i )
	{
		$this->writeByte($i ? 1 : 0);
	}

	public function writeByte( $b )
	{
		fwrite( $this->stream, strrev( pack( "C", $b ) ) );
	}

	public function writeBytes( $byteArray )
	{
		foreach($byteArray as $b){
			$this->writeByte($b);
		}
	}

	//TODO: Java char is unsigned
	public function writeChar( $char )
	{
		$v = ord($char);
		$this->writeByte((0xff & ($v >> 8)));
		$this->writeByte((0xff & $v));
	}

	//TODO: Java short is signed
	public function writeShort( $short )
	{
		$this->writeByte((0xff & ($short >> 8)));
		$this->writeByte((0xff & $short));
	}

	public function writeInt( $i )
	{
		fwrite( $this->stream, pack( "N", $i ), 4 );
	}

	public function writeLong( $l )
	{
		//Todo: test whether this works.
		$bytes = array();
		if($l > 0x7FFFFFFF || $l < -0x80000000){
			if(is_double($l)){
				throw new Exception("WriteLong was called with a double! Are you trying to write 64 bit integers on a 32 bit platform?");
			}else{
				$bytes[0] = $l >> (7*8);
				$bytes[1] = $l >> (6*8);
				$bytes[2] = $l >> (5*8);
				$bytes[3] = $l >> (4*8);
				$bytes[4] = $l >> (3*8);
				$bytes[5] = $l >> (2*8);
				$bytes[6] = $l >> (1*8);
				$bytes[7] = $l >> (0*8);
			}
		}else{
			$rep = ($l > 0) ? 0 : -1;
			$bytes[0] = $rep;
			$bytes[1] = $rep;
			$bytes[2] = $rep;
			$bytes[3] = $rep;
			$bytes[4] = $l >> (3*8);
			$bytes[5] = $l >> (2*8);
			$bytes[6] = $l >> (1*8);
			$bytes[7] = $l >> (0*8);
		}
		foreach($bytes as $b){
			echo $b."<br>";
		}
		$this->writeBytes($bytes);
	}

	public function writeFloat( $d )
	{
		//PHP does not have floats, so we will always send a double
		$this->writeDouble($d);
	}

	public function writeDouble( $d )
	{
		fwrite( $this->stream, strrev( pack( "d", $d ) ) );
	}

	public function writeChars( $string )
	{
		$array = str_split($string);
		foreach($array as &$cur)
		{
			$v = ord($cur);
			$this->writeByte((0xff & ($v >> 8)));
			$this->writeByte((0xff & $v));
		}
	}

	public function writeString( $string )
	{
		$array = str_split($string);
		$this->writeInt(count($array));
		$this->writeChars($string);
	}

	/*********************/
	/****** READING ******/
	/*********************/

	public function readBoolean()
	{
		return $this->readByte() != 0;
	}

	public function readByte()
	{
		$up = unpack( "Ci", fread( $this->stream, 1 ) );
		$b = $up["i"];
		if($b > 127){
			$b -= 256;
		}
		return $b;
	}

	public function readBytes($length)
	{
		$bytes = array();
		for($i = 0;$i<$length;$i++){
			$bytes[$i] = $this->readByte();
		}
		return $bytes;
	}

	public function readUnsignedByte()
	{
		$up = unpack( "Ci", fread( $this->stream, 1 ) );
		$b = $up["i"];
		return $b;
	}

	public function readChar()
	{
		$byte1 = $this->readByte();
		$byte2 = $this->readByte();
		$charValue = chr(utf8_decode((($byte1 << 8) | ($byte2 & 0xff))));
		return $charValue;
	}

	//TODO: implement
	public function readShort()
	{
		$byte1 = $this->readByte();
		$byte2 = $this->readByte();
		$s = (($byte1 & 0xff) << 8) | ($byte2 & 0xff);
		if($s > 32767){
			$s -= 65536;
		}
		return $s;
	}

	public function readInt()
	{
		$a = $this->readByte();
		$b = $this->readByte();
		$c = $this->readByte();
		$d = $this->readByte();
		$i = ((($a & 0xff) << 24) | (($b & 0xff) << 16) | (($c & 0xff) << 8) | ($d & 0xff));
		if($i > 2147483648){
			$i -= 4294967296;
		}
		return $i;
	}

	public function readLong()
	{
		$bytes = array();
		for($i = 0;$i<8;$i++){
			$bytes[$i] = $this->readByte();
		}
		$a = ((($bytes[0] & 0xff) << 24) | (($bytes[1] & 0xff) << 16) | (($bytes[2] & 0xff) << 8) | ($bytes[3] & 0xff));
		$b = ((($bytes[4] & 0xff) << 24) | (($bytes[5] & 0xff) << 16) | (($bytes[6] & 0xff) << 8) | ($bytes[7] & 0xff));
		if($a == -1){
			if($b > 0){
				$b = $b * -1;
			}
			return $b;
		}else if($a != 0){
			if(PHP_INT_SIZE == 8){
				return ($a << 32) | $b;
			}else{
				//Can't handle this. TODO: Might be possible with manual math to string
				throw new Exception("Can't read 64bit long bigger than max value on 32 bit platform!");
			}
		}else{
			return $b;
		}
	}

	public function readDouble()
	{
		$up = unpack( "di", strrev( fread( $this->stream, 8 ) ) );
		$d = $up["i"];
		return $d;
	}

	public function readChars($length)
	{
		$buf = "";
		for($i = 0;$i<$length;$i++)
		{
			$byte1 = $this->readByte();
			$byte2 = $this->readByte();
			$buf = $buf.chr(utf8_decode((($byte1 << 8) | ($byte2 & 0xff))));
		}
		return $buf;
	}

	public function readString()
	{
		$length = $this->readInt();
		return $this->readChars($length);
	}
}
?>
