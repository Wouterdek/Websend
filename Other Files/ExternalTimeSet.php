<<<<<<< HEAD
<?php
    include_once 'Websend.php';
    
    $ws = new Websend("123.456.789.123");
    $ws->connect("password");
    $ws->doCommandAsConsole("time set 6000");
    $ws->disconnect();
=======
<?php
    include_once 'websend.php';
    
	$ws = new Websend("YOUR_IP", 4445);
	$ws->connect("YOUR_PASSWORD");
    $ws->doCommandAsConsole("time set 6000");
    $ws->disconnect();
>>>>>>> Minor changes
?>