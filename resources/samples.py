#!/usr/bin/python


def _sum(a, b):
    return int(a) + int(b)

def avg(*n):
    result = sum(map(int, n)) / len(n)
    return result

# {"module": "samples", "function": "avg", "arguments": [2,4,5,1,23]}
