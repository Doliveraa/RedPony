from distutils.core import setup

setup(
    name='astral',
    version='0.1',
    py_modules=['commands', 'astral'],
    install_requires=[
        'Click',
        'pymongo',
        'pyjwt'
    ],
    entry_points='''
        [console_scripts]
        astral=commands:main
    ''',
)
