#### To Import
# home=`dirname $0`
# . $home/setup.sh
#
# To use:
#
# jar
# classpath
# echoerr "I am a warning to STDERR\n"
#
# Set DEBUG to see debug output and PRINT_ANT to see the ant process output
#
####

echoerr() { 
	if [ -n "$DEBUG" ]; then
		printf "$@" 1>&2; 
	fi
}

jar() {
	echoerr "########################################################\n"
	echoerr "Compiling Healthchecks ..."
	
	if [ -n "$PRINT_ANT" ]; then
		echoerr "\n" #put in a line break now otherwise it looks odd
		ANT_OPTS='-Xmx300M' ant jar
		ant_output=""
	else
		ant_output=$(ANT_OPTS='-Xmx300M' ant jar 2>&1)	
	fi

	if [[ $? != 0 ]]; then
		echoerr "FAILED. Please check the following output for possible reasons:\n"
		echoerr "\n"
		echoerr $ant_output
	fi
	echoerr "Finished\n"
	echoerr "########################################################\n"
}

classpath() {
	home=`dirname $0`
	cp=$home:$home/build/
	for jar in $home/lib/*.jar; do
		if [[ "$jar" == *'ensj-healthcheck.jar' ]]; then
			echo "ERROR: Detected a ensj-healthcheck.jar at $jar. Please remove"
			continue
		fi
    cp=$jar:$cp
	done
	cp=$home/target/dist/ensj-healthcheck.jar:$cp
	cp=$cp:src/:$home/resources/runtime
	export CLASSPATH=$cp
}