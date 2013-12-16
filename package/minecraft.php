<?php
//variables
/*********************************** Variables ***********************************/
/**/  $checkpass = "PutYourPasswordHere";
/*********************************************************************************/
/**/  $receivedMD5 = $_POST['authKey'];
/**/  $args = $_POST["args"]; //each argument is stored in an array called "args"
/**/  $json = json_decode($_POST["jsonData"]);
/*********************************************************************************/

$invoker = $json->{'Invoker'}->{'Name'};

//Do not edit!
if($receivedMD5 != "" && $args[0] != "")
{
    if($receivedMD5 == md5($checkpass))
    {
        //Begin your code here.
        if($args[0] == "checkcolors") //script 1
        {
            if($invoker == 'console')
            {
                print('/Output/PrintToConsole:Error: Only in-game players can use this command.;');
            }else{
                print('/Output/PrintToPlayer:Example script from php.;');
                print('/Output/PrintToPlayer:This command will show all possible colors;');
                // use minecraft color codes to color the text
                print("/Output/PrintToPlayer:&aThis is green;");
                print("/Output/PrintToPlayer:&bThis is light blue;");
                print("/Output/PrintToPlayer:&cThis is red;");
                print("/Output/PrintToPlayer:&dThis is pink;");
                print("/Output/PrintToPlayer:&eThis is yellow;");
                print("/Output/PrintToPlayer:&fThis is white;");
                print("/Output/PrintToPlayer:&1This is dark blue;");
                print("/Output/PrintToPlayer:&2This is dark green;");
                print("/Output/PrintToPlayer:&3This is aqua;");
                print("/Output/PrintToPlayer:&4This is dark red;");
                print("/Output/PrintToPlayer:&5This is purple;");
                print("/Output/PrintToPlayer:&6This is gold;");
                print("/Output/PrintToPlayer:&7This is grey;");
                print("/Output/PrintToPlayer:&8This is dark grey;");
                print("/Output/PrintToPlayer:&9This is blue;");
                print("/Output/PrintToPlayer:&0This is black;");
                print("/Output/PrintToPlayer:&7These are &1multiple colors &cin one &5sentence;");
            }
        }
        elseif($args[0] == "timeset") //script 2
        {
            if($invoker == 'console')
            {
                print('/Output/PrintToConsole:Error: Only in-game players can use this command.;');
            }else{
                print('/Output/PrintToPlayer:Success;');
                print('/Output/PrintToPlayer:Example script from php.;');
                print('/Output/PrintToPlayer:This will set the time of players world to day.;');
                // use /Command/ExecuteBukkitCommand: to indicate a command sent by $invoker.
                // Behind that line you can put any player chat command.
                print("/Command/ExecuteBukkitCommand:time day;");
                print("/Output/PrintToPlayer:Invoker = ".$invoker.";");
                print("/Output/PrintToPlayer:Argument 1 = ".$args[0].";");
            }
        }
        elseif($args[0] == "weatherset") //script 3 example using a plugin command to change the weather
        {
            print("/Command/ExecuteConsoleCommand:weather sun;");
            if($invoker == 'console')
            {
                print('/Output/PrintToConsole:Weather changed.;');
                print("/Output/PrintToConsole:Player = ".$invoker.";");
                print("/Output/PrintToConsole:Argument 1 = ".$args[0].";");
            }else{
                print('/Output/PrintToPlayer:Weather changed.;');
                print("/Output/PrintToPlayer:Player = ".$invoker.";");
                print("/Output/PrintToPlayer:Argument 1 = ".$args[0].";");
            }
        }
        elseif($args[0] == "consoleCommand") //script 4
        {
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This command will send a command to the console.;');
            if($invoker == 'console')
            {
                print('/Output/PrintToConsole:Error: Only in-game players can use this command.;');
            }
            else
            {
                print('/Output/PrintToPlayer:Proof it is send to console:;');
                // use /Command/ExecuteConsoleCommand: to indicate a command from console.
                print("/Command/ExecuteConsoleCommand:say Hello World;");
            }
        }
        else
        {
            print('/Output/PrintToPlayer:Websend: Unknown command.;');
        }
        //Stop editing here.
    }
    else
    {
        print('/Output/PrintToConsole:Authorization Failed;');
        print("/Output/PrintToConsole:$receivedMD5 != ".md5($checkpass).";");
    }
}
else
{
    print("/Output/PrintToConsole:No (enough) data provided.;");
}
?>