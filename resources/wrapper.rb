#!/usr/bin/ruby

require "rubygems"
require "json"

def awaitMessage
  msg = ""
  loop do
    line = STDIN.readline.chomp
    if not line.empty?
      break if line == "end!"
      msg << line
    end
  end
  JSON.parse msg.chomp
end

def run
  loop do

    begin

      message = awaitMessage

      module_name = message['module']
      function = message['function'].to_sym

      mod = require 'resources/' + module_name

      result = nil

      if message.has_key? 'arguments'
        if message['arguments'].kind_of?(Array)
          result = mod.send(function, *message['arguments'])
        else
          result = mod.send(function, message['arguments'])
        end
      else
        result = result = mod.send(function)
      end

      response = {
        :result => result,
        :error => false
      }

    rescue StandardError => e
      response = {
        :error => true,
        :result => e.message
      }

    end

    puts response.to_json
    puts "end!"
    STDOUT.flush

  end

end

run()
