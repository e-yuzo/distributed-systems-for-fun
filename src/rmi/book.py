import Pyro4

@Pyro4.expose
class Book:
    author = ""
    title = ""
    book_cover = ""

    def __init__(self, author, title, cover): 
        self.author = author
        self.title = title
        self.book_cover = cover