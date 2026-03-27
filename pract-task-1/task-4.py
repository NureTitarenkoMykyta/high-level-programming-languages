class Book:
    def __init__(self, title, author, year):
        self.title = title
        self.author = author
        self.year = year

    def __str__(self):
        return f"{self.title} by {self.author} ({self.year})"
    
book1 = Book("To Kill a Mockingbird", "Harper Lee", 1960)
print(book1)