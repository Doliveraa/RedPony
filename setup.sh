#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"


SSL=true
SECRET=""
PORT=-1
while test $# -gt 0; do
        case "$1" in
                -h|--help)
                        echo "$package - ASTRAL"
                        echo " "
                        echo "$package [options]"
                        echo " "
                        echo "options:"
                        echo "-h, --help                show brief help"
                        echo "--ssl                     setup API on SSL port"
                        echo "--secret                  specify a secret phrase"
                        echo "--port                    specify a port"
                        exit 0
                        ;;
                --ssl)
                        SSL=true
                        shift
                        ;;
                --secret*)
                        export SECRET=`echo $1 | sed -e 's/^[^=]*=//g'`
                        shift
                        ;;
                --port*)
                        export PORT=`echo $1 | sed -e 's/^[^=]*=//g'`
                        shift
                        ;;
                *)
                        break
                        ;;
        esac
done

# Add Mongo Keyserver
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6
echo "deb [ arch=amd64,arm64 ] http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.4 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.4.list

# Update apt-get
sudo apt-get update

# Install curl
sudo apt-get install curl

# Install Node.js
cd ~
curl -sL https://deb.nodesource.com/setup_8.x -o nodesource_setup.sh
sudo bash nodesource_setup.sh
sudo apt-get install nodejs
rm nodesource_setup.sh

# Install Python
sudo apt-get install python3 python3-pip

# Install mongodb
sudo apt-get install -y mongodb-org
sudo systemctl start mongod
sudo systemctl enable mongod

# Install astral
cd $SCRIPT_DIR/astral
pip3 install -e . --upgrade --force-reinstall
if ! grep -Fxq ".*_ASTRAL_COMPLETE.*" ~/.bashrc
then
    echo 'eval "$(_ASTRAL_COMPLETE=source astral)"' >> ~/.bashrc
fi

# Generate config file
OPTIONS = "--savedir $SCRIPT_DIR/API/config"
if $SECRET; then
    $OPTIONS .= "$OPTIONS --secret $SECRET"
fi
if $PORT; then
    $OPTIONS .= "$OPTIONS --port $PORT"
fi
if $SSL; then
    $OPTIONS .= "$OPTIONS --ssl"
fi

echo "astral setup_api $OPTIONS"
astral setup_api $OPTIONS

# Install node packages
cd $SCRIPT_DIR/API
npm install
sudo npm install -g forever

# Setup ssl
cd $SCRIPT_DIR/API
read -p "Enter your domain name: " DOMAIN_NAME
sudo apt-get install letsencrypt
sudo certbot certonly --standalone -d $DOMAIN_NAME
sudo cp /etc/live/letsencrypt/$DOMAIN_NAME/privkey.pem $SCRIPT_DIR/API/config/privkey.pem
sudo cp /etc/live/letsencrypt/$DOMAIN_NAME/
