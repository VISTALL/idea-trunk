class Book {
  static belongsTo = [author: Author]
  def name
}

class Author {
  static hasMany = [books: Book]
}

Book.findBy<caret>