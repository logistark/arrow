package arrow.benchmarks

import arrow.effects.IO
import arrow.effects.suspended.fx.Fx
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.CompilerControl
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class DeepBind {

  @Param("20")
  var depth: Int = 0

  fun ioFib(n: Int): IO<Int> =
    if (n <= 1) IO { n }
    else ioFib(n - 1).flatMap { a ->
      ioFib(n - 2).flatMap { b -> IO { a + b } }
    }

  fun fxFib(n: Int): Fx<Int> =
    if (n <= 1) Fx { n }
    else fxFib(n - 1).flatMap { a ->
      fxFib(n - 2).flatMap { b -> Fx { a + b } }
    }

  @Benchmark
  fun fx(): Int =
    Fx.unsafeRunBlocking(fxFib(depth))

  @Benchmark
  fun io(): Int =
    ioFib(depth).unsafeRunSync()

  @Benchmark
  fun cats(): Any =
    arrow.benchmarks.effects.scala.cats.`DeepBind$`.`MODULE$`.fib(depth).unsafeRunSync()

  @Benchmark
  fun zio(): Any =
    arrow.benchmarks.effects.scala.zio.`DeepBind$`.`MODULE$`.fib(depth)
}