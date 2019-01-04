# wry-parser

    Usage: [-hp] -s=SOURCE [-xc=EXCLUDE_COMMAND_FILE] [-xs=EXCLUDE_SUB_NAME_FILE]                
    -h, --help            Displays help.    
    -p, --play            Run debug game in interactive mode after parsing of source
                          file.                          
    -s, --source=SOURCE   Wry Source file.
         -xc, --exclude-commands=EXCLUDE_COMMAND_FILE
                        File with list of BASIC command words to exclude during
                        parsing of Wry Source
                          
          -xs, --exclude-subs=EXCLUDE_SUB_NAME_FILE
                        File with list of sub names to exclude from story pages.
                        




Example run: java -jar wry-parser.jar -s WRY.BAS -xc exclude_commands.list -xs exclude_subs.list -p
