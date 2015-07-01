require 'rubygems'

require 'active_support/core_ext/string/inflections'
require 'aws-sdk'
require 'configs'
require 'logger'
require 'pathname'
require 'rainbow'
require 'rainbow/ext/string'

require 'origami/build'
require 'origami/build_list'
require 'origami/changelog'
require 'origami/s3_package'
require 'origami/version_code'

module Origami
  def self.app_dir
    project_dir + 'app'
  end

  def self.bucket
    'android-ksr-builds'
  end

  def self.project_dir
    Pathname.new(File.dirname(__FILE__)).parent
  end

  def self.log
    return @log if @log

    @log = Logger.new(STDOUT)
    @log.formatter = proc do |severity, datetime, progname, msg|
      time = datetime.strftime("%H:%M:%S")
      "[#{time} Origami]: #{msg}\n"
    end
    @log
  end

  def self.say(message)
    log.info message.color(:green)
  end

  def self.s3_client
    @s3_client ||= Aws::S3::Client.new(
      credentials: Aws::Credentials.new(Configs[:s3][:access_key], Configs[:s3][:secret_key]),
      region: 'us-east-1'
    )
  end
end
