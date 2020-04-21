require 'rubygems'

require 'active_support/core_ext/string/inflections'
require 'aws-sdk'
require 'configs'
require 'logger'
require 'pathname'
require 'rainbow'
require 'rainbow/ext/string'

require 'milkrun/build'
require 'milkrun/build_list'
require 'milkrun/changelog'
require 'milkrun/checkstyle'
require 'milkrun/firebase_credentials_extractor'
require 'milkrun/slack_webhook_extractor'
require 'milkrun/git'
require 'milkrun/hockey_app'
require 'milkrun/i18n_string_resources'
require 'milkrun/lint'
require 'milkrun/s3_package'
require 'milkrun/server_config_refresher'
require 'milkrun/version_code'
require 'milkrun/version_name'

module Milkrun
  def self.assets_dir
    File.join(app_dir, "src/main/assets")
  end

  def self.app_dir
    File.join(project_dir, 'app')
  end

  def self.bucket
    'android-ksr-builds'
  end

  def self.error(message)
    log.error message.color(:red)
  end

  def self.project_dir
    Pathname.new(File.dirname(__FILE__)).parent
  end

  def self.log
    return @log if @log

    @log = Logger.new(STDOUT)
    @log.formatter = proc do |severity, datetime, progname, msg|
      time = datetime.strftime("%H:%M:%S")
      "[#{time} Milkrun]: #{msg}\n"
    end
    @log
  end

  # Prompts a user for a value.
  #
  # Returns the input String.
  def self.prompt(message, label: nil)
    say message
    input_text = label ? "#{label}: " :  "Press enter to confirm"
    print input_text.color(:yellow)
    STDIN.gets.chomp
  end

  def self.say(message)
    log.info message.color(:green)
  end

  def self.s3_client
    @s3_client ||= Aws::S3::Client.new({
       access_key_id:     ENV['AWS_ACCESS_KEY_ID']     || Configs[:s3][:access_key],
       secret_access_key: ENV['AWS_SECRET_ACCESS_KEY'] || Configs[:s3][:secret_key],
       region:            ENV['AWS_REGION']            || 'us-east-1'
     })
  end
end
