numbers_count = 3
numbers = []
while len(numbers) < numbers_count:
    try:
        numbers.append(int(input("Enter a number: ")))
    except ValueError:
        print("Invalid input. Please enter a valid integer.")
print(max(numbers))