module Milkrun
  class SlackWebhookExtractor

    def properties
    lines = File.readlines("../config/slack.properties")
            properties = lines.map { |line| line.strip.split('=') }.to_h
    end

    def webhook
      properties["slack_url"]
    end

  end
end
