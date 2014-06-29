import module namespace bin = "http://basex.org/bin-tree" at "binary-tree.xqm";

declare function local:randomize($seq) {
  for $x in $seq
  order by random:double()
  return $x
};

let $seq := local:randomize(1 to 1000)
return bin:serialize(
  fold-left(
    $seq,
    bin:empty(),
    function($tree, $x) { bin:insert($x, $tree) }
  )
)
