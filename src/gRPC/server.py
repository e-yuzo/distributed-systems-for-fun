
from concurrent import futures
import time
import grpc
import book_pb2
import book_pb2_grpc

_ONE_DAY_IN_SECONDS = 60 * 60 * 24
submitted_books = []
# removed_books = []

class Answer(book_pb2_grpc.RelevantBooksServicer):

    def SubmitBook(self, request, context):
        submitted_books.append(request)
        print(request.title)
        return book_pb2.BookResponse(response=1)

    def GetBooks(self, request, context):
        print("listed")
        bookss = book_pb2.BooksResponse()
        bookss.books.extend(submitted_books)
        # for book in submitted_books:
        #     bookss.books.add(book)
        # return book_pb2.BooksResponse.extend(submitted_books)
        return bookss

    def RemoveBook(self, request, context):
        # old_book = book_pb2.Book
        print("removed")
        for book in submitted_books:
            if book.title == request.title:
                # old_book = book
                submitted_books.remove(book)
                # removed_books.append(book)
        return book_pb2.BookResponse(response=1)
    

# def serve():
#     server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
#     book_pb2_grpc.add_RelevantBooksServicer_to_server(Answer(), server)
#     server.add_insecure_port('[::]:50051')
#     server.start()
#     try:
#         while True:
#         time.sleep(_ONE_DAY_IN_SECONDS)
#     except KeyboardInterrupt:
#         server.stop(0)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    book_pb2_grpc.add_RelevantBooksServicer_to_server(Answer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()