#!/usr/bin/python

import sys
import os.path
import json
import traceback

from pprint import pprint

json_encode = lambda x: json.dumps(x)
json_decode = lambda x: json.loads(x)

def awaitMessage():
    msg = ""
    while True:
        line = sys.stdin.readline().strip()
        if line:
            # with open("/Users/emdagon/dev/test.log", "a") as fd:
            #     fd.write("line input: %s\n" % line)
            if line == "end!":
                break
            msg = msg + line
    return json.loads(msg)

def run():

    while True:

        try:

            message = awaitMessage()

            module_name = message['module']
            function_name = message['function']

            mod = __import__(module_name)

            function = getattr(mod, function_name)

            result = None

            if 'arguments' in message:
                if isinstance(message['arguments'], dict):
                    result = function.__call__(**message['arguments'])
                elif isinstance(message['arguments'], list):
                    result = function.__call__(*message['arguments'])
                else:
                    result = function.__call__(message['arguments'])
            else:
                result = function.__call__()

            response = {
                'result': result,
                'error': False
            }

        except Exception, e:

            response = {
                'error': True,
                'result': traceback.format_exc(e)
            }

        print json_encode(response)
        print "end!"
        sys.stdout.flush()

with open(os.path.expanduser("~/dev/test.log"), "a") as fd:
    fd.write("run python!\n")

run()
