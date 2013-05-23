#!/usr/bin/ruby


def sum(*n)
  n.reduce(:+)
end

def avg(*n)
  result = sum(*n) / n.length
  result
end

def plain(p)
  p
end

# {"module": "samples", "function": "avg", "arguments": [2,4,5,1,23]}
