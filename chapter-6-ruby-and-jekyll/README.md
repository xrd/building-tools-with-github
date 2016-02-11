Run these commands:

```
gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3
\curl -sSL https://get.rvm.io | bash -s stable
. ~/.rvm/scripts/rvm 
rvm install 2.1.0
gem install bundler
bundle
bundle exec ruby run.rb 
```
