require 'net/http'
require 'uri'
require 'json'
require_relative 'secrets'

module Milkrun
  class ServerConfigRefresher
    attr_reader :local

    def initialize(local: false)
      @local = local
    end

    def refresh
      response = Net::HTTP.get_response(url)

      if !response.is_a?(Net::HTTPSuccess)
        Milkrun.say "Response from API: #{response.body}"
        raise "Couldn't download config from server!"
      end

      json = JSON.parse(response.body)
      config = JSON.pretty_generate(json)
      File.write(path, config)

      Milkrun.say "Updated config from server"
    end

    def path
      File.expand_path(File.join(Milkrun.assets_dir, "json/server-config.json"))
    end

    private

    def client_id
      local ? Secrets::Api::Client::LOCAL : Secrets::Api::Client::PRODUCTION
    end

    def host
      local ? "http://api.ksr.test/" : "https://#{Secrets::Api::Endpoint::PRODUCTION}"
    end

    def oauth_token
      ENV["KICKSTARTER_API_ANDROID_OAUTH_TOKEN"]
    end

    def url
      str = "#{host}/v1/app/android/config?all_locales=true&client_id=#{client_id}"
      str += "&oauth_token=#{oauth_token}" unless oauth_token.nil?
      URI.parse(str)
    end
  end
end
