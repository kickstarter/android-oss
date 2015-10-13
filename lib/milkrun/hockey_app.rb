require 'configs'
require 'json'
require 'excon'

module Milkrun
  class HockeyApp
    attr_reader :variant, :version_code, :version_name

    def initialize(variant:, version_code:, version_name:)
      @variant, @version_code, @version_name = variant, version_code, version_name
      raise "Missing HockeyApp token!" if token.empty?
      raise "Missing HockeyApp app_id for variant #{variant}!" if app_id.empty?
    end

    # Creates a new version of the app in HockeyApp.
    def create_version
      Milkrun.say "Creating new version of app in HockeyApp"

      body = {}.tap do |json|
        json[:bundle_version] = version_code
        json[:bundle_short_version] = version_name
        json[:status] = 1
      end

      headers = {}.tap do |h|
        h["X-HockeyAppToken"] = token
        h["Accept"] = "application/json"
        h["Content-Type"] = "application/json"
      end

      url = "#{base_url}/#{app_id}/app_versions/new"
      response = Excon.post(url, body: body.to_json, connect_timeout: 10, headers: headers)
      if response.status != 201
        Milkrun.error response.data.to_s
        raise "Failed to post new version to HockeyApp!"
      end

      Milkrun.say "New version created in HockeyApp"
    end

    protected

    def app_id
      Configs[:hockey_app][variant][:app_id]
    end

    def base_url
      "https://rink.hockeyapp.net/api/2/apps"
    end

    def token
      Configs[:hockey_app][:token]
    end
  end
end
