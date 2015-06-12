namespace :deploy do
  desc "Deploy new internal debug build to S3"
  task :internal_debug do
    version = bump_version_code
  end
end

def bump_version_code
  version = Time.now.to_i
  puts "Bumping version to #{version}"
  File.open("app/version_code.txt", "w") do |f|
    f.puts(version)
  end
  version
end
