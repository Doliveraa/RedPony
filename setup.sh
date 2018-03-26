# Install dependencies
sudo apt-get install python3 python3-pip curl

# Install astral
cd astral
pip3 install .
cd ..

# Install Node.js with nvm
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.8/install.sh | bash
source ~/.bashrc
. ~/.nvm/nvm.sh
nvm install --lts

# Install and start MongoDB
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo "deb http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list
sudo apt-get update
sudo apt-get install -y mongodb-org
sudo systemctl start mongod
sudo systemctl enable mongod

# Generate config file
read -p 'Enter a secret phrase: ' secret_phrase
astral setup-api --port 80 --secret $secret_phrase

# Install node packages
cd API
npm install
npm install forever
