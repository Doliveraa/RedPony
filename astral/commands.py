import click
import astral

@click.group()
def main():
    pass

@click.command(name='get-app-token')
@click.option('--file', default=None)
@click.argument('name')
def get_app_token(file, name):
    astral.get_app_token(file, name)

@click.command(name='add-app')
@click.option('--file', default=None)
@click.argument('name')
def add_app(file, name):
    astral.add_app(file, name)

@click.command(name='setup-api')
@click.option('--port')
@click.option('--secret')
@click.option('--file', default='./config.json')
def setup_api(port, secret, file):
    astral.setup_api(port, secret, file)

@click.command(name='start')
def start():
    astral.run_script('start.sh')

@click.command(name='restart')
def restart():
    astral.run_script('stop.sh')
    astral.run_script('start.sh')

@click.command(name='stop')
def stop():
    astral.run_script('stop.sh')

main.add_command(get_app_token)
main.add_command(add_app)
main.add_command(setup_api)
main.add_command(start)
main.add_command(restart)
main.add_command(stop)
