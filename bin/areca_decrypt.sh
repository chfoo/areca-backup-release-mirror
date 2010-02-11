#!/usr/bin/env bash
####################################################################
#
# This script launches Areca's external decryption tool
#
####################################################################

PROGRAM_DIR=`dirname "$0"`
"${PROGRAM_DIR}"/areca_run.sh com.application.areca.external.CmdLineDeCipher "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9" "${10}" "${11}" "${12}"