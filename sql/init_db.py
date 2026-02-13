import pymysql
import os

def execute_sql_file(filename, connection):
    with open(filename, 'r', encoding='utf-8') as f:
        sql_content = f.read()
        
    cursor = connection.cursor()
    # Split by semicolon, but be careful about stored procedures or comments.
    # For this simple file, splitting by ';' should work mostly, but it's better to just run statement by statement.
    # Actually, let's try to execute statements one by one.
    
    statements = sql_content.split(';')
    for statement in statements:
        if statement.strip():
            try:
                cursor.execute(statement)
            except Exception as e:
                print(f"Error executing statement: {statement[:50]}... \nError: {e}")
    connection.commit()
    cursor.close()

def main():
    try:
        conn = pymysql.connect(
            host='localhost',
            user='root',
            password='zs123',
            database='ry-vue',
            charset='utf8mb4'
        )
        print("Connected to database.")
        
        sql_file = r'e:\vue_package\RuoYi-Vue\sql\credit_reporting_init.sql'
        if os.path.exists(sql_file):
            print(f"Executing {sql_file}...")
            execute_sql_file(sql_file, conn)
            print("SQL execution completed.")
        else:
            print(f"File not found: {sql_file}")
            
        conn.close()
    except Exception as e:
        print(f"Database connection failed: {e}")

if __name__ == "__main__":
    main()
