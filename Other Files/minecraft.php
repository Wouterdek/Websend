<<<<<<< HEAD
<?php
//variables
/*********************************** Variables ***********************************/
/**/  $checkpass = "PutYourPasswordHere";
/*********************************************************************************/
/**/  $receivedMD5 = $_POST['authKey'];
/**/  $player = $_POST["player"];
/**/  $args = $_POST["args"]; //each argument is stored in an array called "args"
/*********************************************************************************/
 
 
//Do not edit!
if($receivedMD5 != "" && $args[0] != "")
{
    if($receivedMD5 == md5($checkpass))
    {
        //Begin your code here.
        if($args[0] == "checkcolors") //script 1
        {
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This command will show different possible colors;');
            // use /Chatcolor-red: to set the text of a sentence to red.
            // Other colors are:
            print("&aThis is green;");
            print("&bThis is light blue;");
            print("&cThis is red;");
            print("&dThis is pink;");
            print("&eThis is yellow;");
            print("&fThis is white;");
            print("&1This is dark blue;");
            print("&2This is dark green;");
            print("&3This is aqua;");
            print("&4This is dark red;");
            print("&5This is purple;");
            print("&6This is gold;");
            print("&7This is grey;");
            print("&8This is dark grey;");
            print("&9This is blue;");
            print("&0This is black;");
            print("&7These are &1multiple colors &cin one &5sentence;");
        }
        elseif($args[0] == "timeset") //script 2
        {
            print('/Output/PrintToPlayer:Success;');
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This will set the time of players world to day.;');
            // use /Command/ExecuteBukkitCommand: to indicate a command sent by $player.
            // Behind that line you can put any player chat command.
            print("/Command/ExecuteBukkitCommand:time day;");
            print("/Output/PrintToPlayer:Player = ".$player.";");
            print("/Output/PrintToPlayer:Argument 1 = ".$args[0].";");
        }
        elseif($args[0] == "weatherset") //script 3
        {
            print('/Output/PrintToPlayer:Success;');
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This will set the weather of players world to sun.;');
            // use /Command/ExecuteBukkitCommand: to indicate a command sent by $player.
            // Behind that line you can put any player chat command.
            print("/Command/ExecuteBukkitCommand:weather sun;");
            print("/Output/PrintToPlayer:Player = ".$player.";");
            print("/Output/PrintToPlayer:Argument 1 = ".$args[0].";");
        }
        elseif($args[0] == "consoleCommand") //script 4
        {
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This command will send a command to the console.;');
            if($player == 'console')
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
    }
}
else
{
    print("/Output/PrintToConsole:No (enough) data provided.;");
}
=======
<?php
//variables
/*********************************** Variables ***********************************/
/**/  $checkpass = "PutYourPasswordHere";
/*********************************************************************************/
/**/  $receivedMD5 = $_POST['authKey'];
/**/  $player = $_POST["player"];
/**/  $args = $_POST["args"]; //each argument is stored in an array called "args"
/*********************************************************************************/
 
 
//Do not edit!
if($receivedMD5 != "" && $args[0] != "")
{
    if($receivedMD5 == md5($checkpass))
    {
        //Begin your code here.
        if($args[0] == "checkcolors") //script 1
        {
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This command will show different possible colors;');
            // use the Minecraft color codes to set the text of a sentence.
            print("&aThis is green;");
            print("&bThis is light blue;");
            print("&cThis is red;");
            print("&dThis is pink;");
            print("&eThis is yellow;");
            print("&fThis is white;");
            print("&1This is dark blue;");
            print("&2This is dark green;");
            print("&3This is aqua;");
            print("&4This is dark red;");
            print("&5This is purple;");
            print("&6This is gold;");
            print("&7This is grey;");
            print("&8This is dark grey;");
            print("&9This is blue;");
            print("&0This is black;");
            print("&7These are &1multiple colors &cin one &5sentence;");
        }
        elseif($args[0] == "timeset") //script 2
        {
            print('/Output/PrintToPlayer:Success;');
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This will set the time of players world to day.;');
            // use /Command/ExecuteBukkitCommand: to indicate a command sent by $player.
            // Behind that line you can put any player chat command.
            print("/Command/ExecuteBukkitCommand:time day;");
            print("/Output/PrintToPlayer:Player = ".$player.";");
            print("/Output/PrintToPlayer:Argument 1 = ".$args[0].";");
        }
        elseif($args[0] == "weatherset") //script 3
        {
            print('/Output/PrintToPlayer:Success;');
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This will set the weather of players world to sun.;');
            // use /Command/ExecuteBukkitCommand: to indicate a command sent by $player.
            // Behind that line you can put any player chat command.
            print("/Command/ExecuteBukkitCommand:weather sun;");
            print("/Output/PrintToPlayer:Player = ".$player.";");
            print("/Output/PrintToPlayer:Argument 1 = ".$args[0].";");
        }
        elseif($args[0] == "consoleCommand") //script 4
        {
            print('/Output/PrintToPlayer:Example script from php.;');
            print('/Output/PrintToPlayer:This command will send a command to the console.;');
            if($player == 'console')
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
    }
}
else
{
    print("/Output/PrintToConsole:No (enough) data provided.;");
}
>>>>>>> Minor changes
?>