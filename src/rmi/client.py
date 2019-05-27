from __future__ import print_function
import random
import Pyro4
from book import Book

def class_to_dict(obj):
    # print("{serializer hook, converting to dict: %s}" % obj)
    return {
        "class": "Book",
        "author": obj.author,
        "title": obj.title,
        "cover": obj.book_cover
    }

def dict_to_class(d):
    # print("{deserializer hook, converting to class: %s}" % d)
    return Book(d["author"], d["title"], d["cover"])

def main():
    # Pyro4.locateNS("127.0.0.1", 9090)
    lib = Pyro4.Proxy("PYRONAME:bibliotheque")
    toQuit = "n"
    while toQuit != "y":
        cmd = input("[add|rm|ls]: ")

        if cmd == "add":
            author = input("Author: ")
            title = input("Title: ")
            cover = input("Cover: ")
            b = Book(author, title, cover)
            d = class_to_dict(b)
            lib.addBook(d)

        elif cmd == "rm":
            title = input("Remove this title: ")
            b = Book("", title, "")
            d = class_to_dict(b)
            lib.rmBook(d)

        elif cmd == "ls":
            books = lib.listBooks()
            for book in books:
                b = dict_to_class(book)
                print("Author: " + b.author + " Title: " + b.title + " Cover: " + b.book_cover)

        toQuit = input("Quit? [y/n]: ")
    
    print("the end")

if __name__ == "__main__":
    main()