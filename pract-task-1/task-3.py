from datetime import datetime

current_year = datetime.now().year
year_of_birth = None
while year_of_birth is None:
    try:
        year_of_birth = int(input("Enter your year of birth: "))
    except ValueError:
        print("Invalid input. Please enter a valid integer.")
print(f"You are {current_year - year_of_birth} years old.")