import click
import astral

@click.group()
def main():
    pass

@click.command(name='get-app-token')
@click.option('--file', default=None, help="File to write token to.",
              metavar="<filepath>")
@click.argument('app')
def get_app_token(file, app):
    """Gets the json web token for APP."""
    astral.get_app_token(app, file)

@click.command(name='add-app')
@click.option('--file', default=None, help="File to write token to.",
              metavar="<filepath>")
@click.argument('app')
def add_app(file, app):
    """Adds APP and returns its json web token."""
    astral.add_app(app, file)

@click.command(name='setup-api')
@click.option('--port', default=None, help="Port to setup API at.", metavar="<integer>")
@click.option('--secret', default=None, help="Secret to use for json web tokens",
              metavar="<string>")
@click.option('--savedir', help="Directory to write configs to.", metavar="<filepath>")
@click.option('--ssl', is_flag=True, help="Setup API on ssl port (443)", metavar="<filepath>")
def setup_api(port, secret, savedir, ssl):
    """Creates api config at FILE with PORT and SECRET."""
    if ssl and port != 443:
        raise Exception("SSL requires port 443")
    if not port and ssl: port = 443
    astral.setup_api(port, secret, savedir)

@click.command(name='start')
def start():
    """Starts the api."""
    astral.run_script('start.sh')

@click.command(name='restart')
def restart():
    """Restarts the api."""
    astral.run_script('stop.sh')
    astral.run_script('start.sh')

@click.command(name='stop')
def stop():
    """Stops the api."""
    astral.run_script('stop.sh')

main.add_command(get_app_token)
main.add_command(add_app)
main.add_command(setup_api)
main.add_command(start)
main.add_command(restart)
main.add_command(stop)
