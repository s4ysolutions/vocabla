The problem of mixing givens and ZIO layers is given instances
expose the ZIO dependency to the call-site.

For example, http handler needs to define its dependency on MvStore instead of
requesting just needed use-case