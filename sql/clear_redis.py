import redis

def clear_menu_cache():
    try:
        # Connect to Redis
        r = redis.Redis(host='localhost', port=6379, db=0, password=None)
        
        # Check connection
        r.ping()
        print("Connected to Redis.")

        # Find keys
        keys = r.keys('sys_menu*')
        if keys:
            print(f"Found {len(keys)} menu cache keys.")
            # Delete keys
            r.delete(*keys)
            print("Cleared menu cache.")
        else:
            print("No menu cache keys found.")

    except Exception as e:
        print(f"Redis error: {e}")
        print("Please ensure Redis is running on localhost:6379.")

if __name__ == "__main__":
    clear_menu_cache()
