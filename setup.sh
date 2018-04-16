#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

RED='\033[0;31m'
BLUE='\033[0;34m'
GREEN='\033[0;32m'
WHITE='\033[0;37m'
NC='\033[0m'

SSL=false
SECRET=""
PORT=""
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
                        shift
                        export SECRET=`echo $1 | sed -e 's/^[^=]*=//g'`
                        shift
                        ;;
                --port*)
                        shift
                        export PORT=`echo $1 | sed -e 's/^[^=]*=//g'`
                        shift
                        ;;
                *)
                        break
                        ;;
        esac
done

# Install curl
if [[ -z `which curl` ]]; then
    printf "${WHITE}Installing curl${NC}\n"
    sudo apt-get update >/dev/null
    sudo apt-get install curl >/dev/null
fi

# Install Node.js
if [[ -z `which nodejs` ]]; then
    printf "${WHITE}Installing NodeJS${NC}\n"
    cd ~
    curl -sL https://deb.nodesource.com/setup_8.x -o nodesource_setup.sh >/dev/null
    sudo bash nodesource_setup.sh >/dev/null
    sudo apt-get install nodejs >/dev/null
    rm nodesource_setup.sh >/dev/null
fi

# Install Python
if [[ -z `which python3` ]]; then
    printf "${WHITE}Installing Python${NC}\n"
    sudo apt-get install python3 >/dev/null
fi
if [[ -z `which pip3` ]]; then
    printf "${WHITE}Installing PIP3${NC}\n"
    sudo apt-get install python3-pip >/dev/null
fi

# Install mongodb
if [[ -z `which mongod` ]]; then
    printf "${WHITE}Installing MongoDB${NC}"
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6 >/dev/null
    echo "deb [ arch=amd64,arm64 ] http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.4 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.4.list >/dev/null
    sudo apt-get update >/dev/null

    sudo apt-get install -y mongodb-org >/dev/null
    sudo systemctl start mongod >/dev/null
    sudo systemctl enable mongod >/dev/null
fi

# Install astral
printf "${WHITE}Installing Astral${NC}"
cd $SCRIPT_DIR/astral
pip3 install -e . --upgrade --force-reinstall >/dev/null
if ! grep -Fxq ".*_ASTRAL_COMPLETE.*" ~/.bashrc
then
    echo 'eval "$(_ASTRAL_COMPLETE=source astral)"' >> ~/.bashrc
fi

# Generate config file
OPTIONS="--savedir $SCRIPT_DIR/API/config"
if [[ ! -z $SECRET ]]; then
    OPTIONS=$OPTIONS" --secret $SECRET"
fi
if [[ ! -z $PORT ]]; then
    OPTIONS=$OPTIONS" --port $PORT"
fi
if $SSL; then
    OPTIONS=$OPTIONS" --ssl"
fi
astral setup-api $OPTIONS

# Install node packages
printf "${WHITE}Installing Astral API Dependencies${NC}\n"
cd $SCRIPT_DIR/API
npm install >/dev/null &>/dev/null
sudo npm install -g forever >/dev/null &>/dev/null

# Setup ssl
if $SSL; then
    printf "${GREEN}Astral Installed, Let's setup SSL${NC}"
    cd $SCRIPT_DIR/API
    read -p "Enter your domain name: " DOMAIN_NAME
    sudo apt-get install letsencrypt >/dev/null
    sudo certbot certonly --standalone -d $DOMAIN_NAME
    sudo cp /etc/live/letsencrypt/$DOMAIN_NAME/privkey.pem $SCRIPT_DIR/API/config/privkey.pem
    sudo cp /etc/live/letsencrypt/$DOMAIN_NAME/
fi

printf "${BLUE}Astral installed! use 'astral --help' to start.${NC}\n"
