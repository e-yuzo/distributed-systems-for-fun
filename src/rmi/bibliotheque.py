from __future__ import print_function
import Pyro4

@Pyro4.expose
class Bibliotheque(object):

    books = []

    def __init__(self):
        self.books = []

    def add(self, book):
        self.books.append(book)
        print("added")
    
    def rm(self, book):
        for b in self.books:
            if b.title == book.title:
                self.books.remove(b)
        print("removed")

    def ls(self):
        return self.books