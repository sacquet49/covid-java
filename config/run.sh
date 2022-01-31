# installation de node
sudo apt-get install nodejs

# installation de nginx
sudo apt-get install nginx
sudo nano /etc/nginx/conf.d/default.conf
sudo systemctl restart nginx

# installation de certbot + certbot-nginx
sudo apt install python3 python3-venv libaugeas0
sudo python3 -m venv /opt/certbot/
sudo /opt/certbot/bin/pip install --upgrade pip
sudo /opt/certbot/bin/pip install certbot certbot-nginx
sudo certbot --nginx -d www.sacquet-covid.link

# installation de docker
sudo yum install docker -y
sudo service docker start
sudo systemctl enable docker
sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# installation application
cd /appRoot
sudo docker-compose up
cp target/covid-0.0.1-SNAPSHOT.jar docker/covid_app/jar/covid.jar
cd docker
sudo docker-compose up
sudo docker start covid-web_run

# Check si l'application fonctionne
www.sacquet-covid.link/open/api/swagger-ui-custom.html
