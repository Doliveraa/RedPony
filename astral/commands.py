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


main.add_command(get_app_token)
main.add_command(add_app)
