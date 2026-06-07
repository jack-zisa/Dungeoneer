import sqlite3
import pandas as pd
import os

folder_path: str = "dump_output"
os.makedirs(folder_path, exist_ok=True)

connection = sqlite3.connect('server/run/dungeoneer.db')

print('Dumping server sessions...')
df = pd.read_sql_query("SELECT * FROM server_sessions", connection)
df.to_csv('dump_output/server_sessions.csv', index=False)

print('Dumping client sessions...')
df = pd.read_sql_query("SELECT * FROM client_sessions", connection)
df.to_csv('dump_output/client_sessions.csv', index=False)

print('Dumping factions...')
df = pd.read_sql_query("SELECT * FROM factions", connection)
df.to_csv('dump_output/factions.csv', index=False)

print('Dumping accounts...')
df = pd.read_sql_query("SELECT * FROM accounts", connection)
df.to_csv('dump_output/accounts.csv', index=False)

print('Dumping characters...')
df = pd.read_sql_query("SELECT * FROM characters", connection)
df.to_csv('dump_output/characters.csv', index=False)

print('Dumping raids...')
df = pd.read_sql_query("SELECT * FROM raids", connection)
df.to_csv('dump_output/raids.csv', index=False)

connection.close()
