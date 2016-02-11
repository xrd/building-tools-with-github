Run these commands:

```
gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3
\curl -sSL https://get.rvm.io | bash -s stable
. ~/.rvm/scripts/rvm 
rvm install 2.1.0
gem install bundler
brew install icu4c || sudo apt-get install libicu-dev
brew install cmake || sudo apt-get install cmake
bundle
mkdir ../../sample-wiki
pushd ../../sample-wiki
git init .
git add .
popd
bundle exec ruby hi.rb ../../sample-wiki
```

This represents a slight change from the printed text. The last few lines
create a new repository, then we pass it as parameter to the script (since this
script already resides in a repository...)
