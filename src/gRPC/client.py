
import grpc
import book_pb2
import book_pb2_grpc
import string

def commandHandler(cmd, stub):
    if cmd == "add":
        author = input("Author: ")
        title = input("Title: ")
        book_cover = input("Book Cover: ")
        book = book_pb2.Book(author=author, title=title, book_cover=book_cover)
        response = stub.SubmitBook(book)
        return "Book added: " + str(response.response)

    elif cmd == "rm":
        title = input("Remove this title: ")
        book = book_pb2.Book(author="", title=title, book_cover="")
        response = stub.RemoveBook(book)
        return "Book removed: " + str(response.response)

    elif cmd == "ls":
        # bq = book_pb2.BookQuery(id=1)
        book_list = stub.GetBooks(book_pb2.BookQuery(id=1))
        for book in book_list.books:
            print("Title: " + book.title + " Author: " + book.author + " Cover: " + book.book_cover)
        return "End listing"

def run():
    channel = grpc.insecure_channel('127.0.0.1:50051')
    stub = book_pb2_grpc.RelevantBooksStub(channel)
    while True:
        command = input("Command [add/rm/ls]:")
        print(commandHandler(command, stub))
        if(input("Quit [y/n]: ") == "y"):
            break
    print("the end")

if __name__ == '__main__':
    run()