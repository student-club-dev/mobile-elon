#!/usr/bin/env ruby
require 'xcodeproj'
require 'json'

puts "🔧 iOS Firebase Setup Automating..."
puts ""

PROJECT_PATH = 'iosApp.xcodeproj'
INFO_PLIST = 'iosApp/Info.plist'
PLIST_FILE = 'iosApp/GoogleService-Info.plist'

# TODO: Bu qiymatlar hali ESKI Firebase loyihasidan (studentclubs-d2905, sender 570659158152).
# `uz.elonuz.ios` bundle'ini `elonuz-5dcca` loyihasiga qo'shgach, yangi GoogleService-Info.plist'dagi
# GOOGLE_APP_ID va REVERSED_CLIENT_ID bilan almashtiring.
APP_ID = '1:570659158152:ios:86c6d422fb25ed88ac873d'
REVERSED_CLIENT_ID = 'com.googleusercontent.apps.570659158152-86c6d422fb25ed88ac873d'

# 1. Open project
begin
  project = Xcodeproj::Project.open(PROJECT_PATH)
  puts "✅ Project opened: #{PROJECT_PATH}"
rescue => e
  puts "❌ Error opening project: #{e.message}"
  exit 1
end

target = project.targets.first
puts "✅ Target selected: #{target.name}"
puts ""

# 2. Add Firebase SDK (SPM)
puts "📦 Adding Firebase SDK..."
begin
  firebase_pkg = project.new(Xcodeproj::Project::Object::XCRemoteSwiftPackageReference)
  firebase_pkg.repositoryURL = 'https://github.com/firebase/firebase-ios-sdk.git'
  firebase_pkg.requirement = {
    'kind' => 'upToNextMajorVersion',
    'minimumVersion' => '10.0.0'
  }
  
  firebase_dependency = project.new(Xcodeproj::Project::Object::XCSwiftPackageDependency)
  firebase_dependency.package = firebase_pkg
  firebase_dependency.requirement = firebase_pkg.requirement
  firebase_dependency.product_name = 'FirebaseAuth'
  
  target.package_product_dependencies << firebase_dependency
  puts "✅ Firebase SDK added (FirebaseAuth)"
rescue => e
  puts "⚠️  Firebase might already exist: #{e.message}"
end

# 3. Add GoogleSignIn SDK (SPM)
puts "📦 Adding GoogleSignIn SDK..."
begin
  google_pkg = project.new(Xcodeproj::Project::Object::XCRemoteSwiftPackageReference)
  google_pkg.repositoryURL = 'https://github.com/google/GoogleSignIn-iOS.git'
  google_pkg.requirement = {
    'kind' => 'upToNextMajorVersion',
    'minimumVersion' => '7.0.0'
  }
  
  google_dependency = project.new(Xcodeproj::Project::Object::XCSwiftPackageDependency)
  google_dependency.package = google_pkg
  google_dependency.requirement = google_pkg.requirement
  google_dependency.product_name = 'GoogleSignIn'
  
  target.package_product_dependencies << google_dependency
  puts "✅ GoogleSignIn SDK added"
rescue => e
  puts "⚠️  GoogleSignIn might already exist: #{e.message}"
end

puts ""
puts "💾 Saving project..."
project.save
puts "✅ Project saved"
puts ""

# 4. Add GoogleService-Info.plist to target
puts "📄 Adding plist to target resources..."
begin
  # Find or create file reference
  plist_ref = project.files.find { |f| f.real_path.to_s.include?('GoogleService-Info.plist') }
  
  if plist_ref
    target.resources_build_phase.add_file_reference(plist_ref) unless target.resources_build_phase.files.include?(plist_ref)
    puts "✅ plist added to resources"
  else
    puts "⚠️  plist file reference not found (might need manual addition)"
  end
  
  project.save
rescue => e
  puts "⚠️  Error adding plist: #{e.message}"
end

puts ""
puts "✨ iOS Setup tayyoq!"
