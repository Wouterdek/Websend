Go to http://dev.bukkit.org/server-mods/websend/pages/ for information on syntax and more.

Scripts like the minecraft.php are used in a bukkit -> php -> bukkit connection. These are triggered by either someone typing /ws or /websend. Other events (like player join, player bed enter, player bucket empty, ...) are available with the WSEvents plugin

Scripts like the supplied "ExternalTimeSet.php" can be used to send a command to the bukkit server, without needing an event from the bukkit server first.
This is useful for applications like registration forms (whitelisting), online server setting adjustment (time set), ect...
To be able to use this, add WEBLISTENER_ACTIVE=true in the plugin settings.

To make a Java script (not javascript), go to the scripts folder in the websend directory and make a new folder.
Inside that folder, place a info.txt and a Main.java
The Main.java needs a "run()" function to be able to be called.
Inside the info.txt put "INVOKEONLOAD=true" (no quotes) if you want the script to run when loaded.