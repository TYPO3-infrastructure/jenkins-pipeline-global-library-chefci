#!/opt/chefdk/embedded/bin/ruby

# This small helper reads the metadata.rb file in the current directory and outputs
# the field requested as argv1. Usage:
#
# (cookbook-dir)$ /opt/chefdk/embedded/bin/ruby helpers/ruby/cookbook-metadata.rb version
# 1.2.3
#
# see https://github.com/opscode/chef/blob/master/lib/chef/cookbook/metadata.rb
require 'chef/cookbook/metadata'

field = ARGV.first || "version"

# read in metadata
metadata = Chef::Cookbook::Metadata.new
metadata.from_file('metadata.rb')

# output requested field (e.g. version, name)
puts metadata.send(field)
