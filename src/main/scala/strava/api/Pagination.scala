package strava.api

import cats.effect.Sync
import cats.syntax.all.*
import strava.core.StravaError

/**
 * Pagination utilities for working with Strava API paginated responses
 */
object Pagination:

  /**
   * Fetch all pages of results automatically
   * 
   * @param fetchPage Function to fetch a single page given page number
   * @param perPage Number of items per page (max 200)
   * @param maxPages Maximum number of pages to fetch (safety limit)
   * @return All results combined from all pages
   */
  def fetchAll[F[_], T](
    fetchPage: (Int, Int) => F[Either[StravaError, List[T]]],
    perPage: Int = 200,
    maxPages: Int = 100
  )(using F: Sync[F]): F[Either[StravaError, List[T]]] =
    def loop(page: Int, accumulated: List[T]): F[Either[StravaError, List[T]]] =
      if page > maxPages then F.pure(Right(accumulated))
      else
        fetchPage(page, perPage).flatMap:
          case Right(results) if results.isEmpty =>
            // No more results, we're done
            F.pure(Right(accumulated))
          case Right(results) =>
            // Continue fetching next page
            loop(page + 1, accumulated ++ results)
          case Left(error) =>
            // Error occurred, return what we have so far or the error
            if accumulated.isEmpty then F.pure(Left(error))
            else F.pure(Right(accumulated)) // Return partial results if we already have some

    loop(1, List.empty)

  /**
   * Fetch pages lazily using a stream-like approach
   * Returns a function that can be called to get the next page
   * 
   * @param fetchPage Function to fetch a single page
   * @param perPage Items per page
   * @return Iterator-like function for pagination
   */
  def paginate[F[_], T](
    fetchPage: (Int, Int) => F[Either[StravaError, List[T]]],
    perPage: Int = 30
  )(using F: Sync[F]): F[PaginationIterator[F, T]] =
    F.pure(PaginationIterator[F, T](fetchPage, perPage))

  /**
   * Iterator-like class for manual pagination control
   */
  class PaginationIterator[F[_], T](
    fetchPage: (Int, Int) => F[Either[StravaError, List[T]]],
    perPage: Int
  )(using F: Sync[F]):
    private var currentPage = 0
    private var hasMore = true

    /** Get the next page of results */
    def next(): F[Option[Either[StravaError, List[T]]]] =
      if !hasMore then F.pure(None)
      else
        currentPage += 1
        fetchPage(currentPage, perPage).map:
          case Right(results) if results.isEmpty =>
            hasMore = false
            None
          case Right(results) =>
            Some(Right(results))
          case Left(error) =>
            hasMore = false
            Some(Left(error))

    /** Check if there are more pages available */
    def hasNext: Boolean = hasMore

    /** Reset the iterator to start from the beginning */
    def reset(): Unit =
      currentPage = 0
      hasMore = true

  /**
   * Fetch a specific page range
   * 
   * @param fetchPage Function to fetch a single page
   * @param fromPage Starting page (inclusive)
   * @param toPage Ending page (inclusive)
   * @param perPage Items per page
   * @return Combined results from the specified page range
   */
  def fetchRange[F[_], T](
    fetchPage: (Int, Int) => F[Either[StravaError, List[T]]],
    fromPage: Int,
    toPage: Int,
    perPage: Int = 200
  )(using F: Sync[F]): F[Either[StravaError, List[T]]] =
    require(fromPage > 0, "fromPage must be positive")
    require(toPage >= fromPage, "toPage must be >= fromPage")

    def loop(page: Int, accumulated: List[T]): F[Either[StravaError, List[T]]] =
      if page > toPage then F.pure(Right(accumulated))
      else
        fetchPage(page, perPage).flatMap:
          case Right(results) =>
            loop(page + 1, accumulated ++ results)
          case Left(error) =>
            if accumulated.isEmpty then F.pure(Left(error))
            else F.pure(Right(accumulated))

    loop(fromPage, List.empty)
