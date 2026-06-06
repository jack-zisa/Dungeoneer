import sqlite3
import pandas as pd
import os

folder_path: str = "dump_output"
os.makedirs(folder_path, exist_ok=True)

connection = sqlite3.connect('server/run/dungeoneer.db')

print('Dumping server sessions...')
sessions_df = pd.read_sql_query("SELECT * FROM server_sessions", connection)
sessions_df.to_csv('dump_output/server_sessions.csv', index=False)

print('Dumping client sessions...')
sessions_df = pd.read_sql_query("SELECT * FROM client_sessions", connection)
sessions_df.to_csv('dump_output/client_sessions.csv', index=False)

print('Dumping factions...')
accounts_df = pd.read_sql_query("SELECT * FROM factions", connection)
accounts_df.to_csv('dump_output/factions.csv', index=False)

print('Dumping accounts...')
accounts_df = pd.read_sql_query("SELECT * FROM accounts", connection)
accounts_df.to_csv('dump_output/accounts.csv', index=False)

print('Dumping characters...')
accounts_df = pd.read_sql_query("SELECT * FROM characters", connection)
accounts_df.to_csv('dump_output/characters.csv', index=False)

connection.close()
