<?php
	/*
	 *  Writted by Waterflames for the Websend project
	 *  (Project page: http://dev.bukkit.org/bukkit-plugins/websend/)
	 *  See https://github.com/Waterflames/Websend/blob/master/package/Websend.php for commits by other users.
	 */
	class Websend
	{
		public $timeout = 3600;/* Connection timeout as defined in fsockopen */
		public $password = "";/* Password in Websend server config */
		public $hashAlgorithm = "sha512";
		
		var $host;
		var $port;
		var $stream;

		public function __construct($host, $port = 4445) 
		{
			$this->host = $host;
			$this->port = $port;
		}

		/**
		* Connects to a Websend server.
		* Returns true if successful.
		*/
		public function connect()
		{
			$this->stream = fsockopen($this->host, $this->port,$errno,$errstr,$this->timeout);
			if($this->stream){
				$this->writeRawByte(21);
				$this->writeString("websendmagic");
				$seed = $this->readRawInt();
				$hashedPassword = hash($this->hashAlgorithm, $seed.$this->password);
				$this->writeString($hashedPassword);
				$result = $this->readRawInt();
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
			$this->writeRawByte(20);
		}

		//NETWORK IO
		private function writeRawInt( $i )
		{
			fwrite( $this->stream, pack( "N", $i ), 4 );
		}
	
		private function writeRawDouble( $d )
		{
			fwrite( $this->stream, strrev( pack( "d", $d ) ) );
		}

		private function writeRawByte( $b )
		{
			fwrite( $this->stream, strrev( pack( "C", $b ) ) );
		}

		private function writeChar( $char )
		{
			$v = ord($char);
			$this->writeRawByte((0xff & ($v >> 8)));
			$this->writeRawByte((0xff & $v));
		}

		private function writeChars( $string )
		{
			$array = str_split($string);
			foreach($array as &$cur)
			{
				$v = ord($cur);
				$this->writeRawByte((0xff & ($v >> 8)));
				$this->writeRawByte((0xff & $v));
			}
		}

		private function writeString( $string )
		{
			$array = str_split($string);
			$this->writeRawInt(count($array));
			foreach($array as &$cur)
			{
				$v = ord($cur);
				$this->writeRawByte((0xff & ($v >> 8)));
				$this->writeRawByte((0xff & $v));
			}
		}

		private function readRawInt()
		{
			$a = $this->readRawByte();
			$b = $this->readRawByte();
			$c = $this->readRawByte();
			$d = $this->readRawByte();
			$i = ((($a & 0xff) << 24) | (($b & 0xff) << 16) | (($c & 0xff) << 8) | ($d & 0xff));
			if($i > 2147483648){
	 			$i -= 4294967296;
 			}
			return $i;
		}
		private function readRawDouble()
		{
			$up = unpack( "di", strrev( fread( $this->stream, 8 ) ) );
			$d = $up["i"];
			return $d;
		}
		private function readRawByte()
		{
			$up = unpack( "Ci", fread( $this->stream, 1 ) );
			$b = $up["i"];
			if($b > 127){
				$b -= 256;
			}
			return $b;
		}
		private function readRawUnsignedByte()
		{
			$up = unpack( "Ci", fread( $this->stream, 1 ) );
			$b = $up["i"];
			return $b;
		}
		private function readChar()
		{
			$byte1 = $this->readRawByte();
			$byte2 = $this->readRawByte();
			$charValue = chr(utf8_decode((($byte1 << 8) | ($byte2 & 0xff))));
			return $charValue;
		}
		private function readChars($len)
		{
			$buf = "";
			for($i = 0;$i<$len;$i++)
			{
				$byte1 = $this->readRawByte();
				$byte2 = $this->readRawByte();
				$buf = $buf.chr(utf8_decode((($byte1 << 8) | ($byte2 & 0xff))));
			}
			return $buf;
		}

		//WEBSEND SPECIFIC

		/**
		* Run a command as if the specified player typed it into the chat.
		*
		* @param string $cmmd Command and arguments to run.
		* @param string $playerName Exact name of the player to run it as.
		* @return true if the command and player were found, else false
		*/
		public function doCommandAsPlayer($cmmd, $playerName)
		{
			$this->writeRawByte(1);
			$this->writeString($cmmd);
			if(isset($playerName))
			{
				$this->writeString($playerName);
			}
			else
			{
				$this->writeString("null");
			}

			if($this->readRawInt() == 1)
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
			$this->writeRawByte(2);
			$this->writeString($cmmd);

			if($this->readRawInt() == 1)
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
			$this->writeRawByte(3);
			$this->writeString($scriptName);
		}
		
		/**
		* Start plugin output capture
		*
		* @param string $pluginName Name of the plugin.
		*/
		public function startPluginOutputListening($pluginName)
		{
			$this->writeRawByte(4);
			$this->writeString($pluginName);
		}
		
		/**
		* Stop plugin output capture
		*
		* @param string $pluginName Name of the plugin.
		* @return array of strings that contains output.
		*/
		public function stopPluginOutputListening($pluginName)
		{
			$this->writeRawByte(5);
			$this->writeString($pluginName);
			$size = $this->readRawInt();
			$data = array();
			for($i = 0; $i<$size;$i++){
				$messageSize = $this->readRawInt();
				$data[$i] = $this->readChars($messageSize);
			}
			return $data;
		}
		
		/**
		* Print output to the console window. Invisible to players.
		*/
		public function writeOutputToConsole($message)
		{
			$this->writeRawByte(10);
			$this->writeString($message);
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
			$this->writeRawByte(11);
			$this->writeString($message);
			if(isset($playerName))
			{
				$this->writeString($playerName);
			}
			else
			{
				$this->writeString("null");
			}

			if($this->readRawInt() == 1)
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
			$this->writeRawByte(12);
			$this->writeString($message);
		}
	}
?>
