#### To Import
# home=`dirname $0`
# . $home/setup.sh
#
# To use:
#
# jar
# classpath
# echoerr "I am a warning to STDERR\n"
####

echoerr() { printf "$@" 1>&2; }

jar() {
	echoerr "########################################################\n"
	echoerr "Compiling Healthchecks ... "
	ant_output=$(ANT_OPTS='-Xmx300M' ant jar 2>&1)
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
    cp=$jar:$cp
	done
	cp=$home/target/dist/ensj-healthcheck.jar:$cp
	cp=$cp:src/:$home/resources/runtime
	export CLASSPATH=$cp
}