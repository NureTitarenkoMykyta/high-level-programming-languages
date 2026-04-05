import math

numbers = list(range(1, 21))

for i in numbers:
    print(f"{i}, {int(math.pow(i, 2))}")
