
#run python3 -m Pyro4.naming
from __future__ import print_function
import time
import Pyro4
from bibliotheque import Bibliotheque
from book import Book

@Pyro4.expose
@Pyro4.behavior(instance_mode="single")
class Library(object):

    library = Bibliotheque()

    def addBook(self, book):
        b = self.dict_to_class(book)
        self.library.add(b)

    def rmBook(self, book):
        b = self.dict_to_class(book)
        self.library.rm(b)

    def listBooks(self):
        list_of_books = []
        for book in self.library.books:
            b = self.class_to_dict(book)
            list_of_books.append(b)
        return list_of_books

    def class_to_dict(self, obj):
        # print("{serializer hook, converting to dict: %s}" % obj)
        return {
            "class": "Book",
            "author": obj.author,
            "title": obj.title,
            "cover": obj.book_cover
        }

    def dict_to_class(self, d):
        # print("{deserializer hook, converting to class: %s}" % d)
        return Book(d["author"], d["title"], d["cover"])

Pyro4.Daemon.serveSimple({
    Library: "bibliotheque"
})