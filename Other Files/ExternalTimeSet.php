<?php
    include_once 'Websend.php';
    
    $ws = new Websend("123.456.789.123");
    $ws->connect("password");
    $ws->doCommandAsConsole("time set 6000");
    $ws->disconnect();
?>