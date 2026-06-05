import sqlite3
import pandas as pd
import os

folder_path: str = "dump_output"
os.makedirs(folder_path, exist_ok=True)

connection = sqlite3.connect('server/run/dungeoneer.db')

print('Dumping accounts...')
accounts_df = pd.read_sql_query("SELECT * FROM accounts", connection)
accounts_df.to_csv('dump_output/accounts.csv', index=False)

print('Dumping sessions...')
sessions_df = pd.read_sql_query("SELECT * FROM sessions", connection)
sessions_df.to_csv('dump_output/sessions.csv', index=False)

connection.close()
